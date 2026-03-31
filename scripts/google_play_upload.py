#!/usr/bin/env python3
"""Google Play Store upload script using Android Publisher API v3."""

import argparse, glob, json, os, sys, time
import jwt, requests

API_BASE = 'https://androidpublisher.googleapis.com/androidpublisher/v3'
UPLOAD_BASE = 'https://androidpublisher.googleapis.com/upload/androidpublisher/v3'
SA_KEY_PATH = os.path.expanduser('~/.openclaw/secrets/google-play-service-account.json')

IMAGE_TYPES = [
    'phoneScreenshots', 'sevenInchScreenshots', 'tenInchScreenshots',
    'tvScreenshots', 'wearScreenshots',
]
ASSET_TYPES = ['icon', 'featureGraphic', 'promoGraphic', 'tvBanner']

class GooglePlayUploader:
    def __init__(self, package_name):
        self.package_name = package_name
        self.token = None
        self.token_expiry = 0
        self.edit_id = None

    def authenticate(self):
        if self.token and time.time() < self.token_expiry - 60:
            return
        sa = json.load(open(SA_KEY_PATH))
        now = int(time.time())
        payload = {
            'iss': sa['client_email'],
            'scope': 'https://www.googleapis.com/auth/androidpublisher',
            'aud': 'https://oauth2.googleapis.com/token',
            'iat': now,
            'exp': now + 3600,
        }
        signed = jwt.encode(payload, sa['private_key'], algorithm='RS256')
        resp = requests.post('https://oauth2.googleapis.com/token', data={
            'grant_type': 'urn:ietf:params:oauth:grant-type:jwt-bearer',
            'assertion': signed,
        })
        resp.raise_for_status()
        self.token = resp.json()['access_token']
        self.token_expiry = now + 3600

    def headers(self):
        self.authenticate()
        return {'Authorization': f'Bearer {self.token}'}

    def create_edit(self):
        resp = requests.post(
            f'{API_BASE}/applications/{self.package_name}/edits',
            headers={**self.headers(), 'Content-Type': 'application/json'},
            json={}
        )
        resp.raise_for_status()
        self.edit_id = resp.json()['id']
        print(f'Created edit: {self.edit_id}')

    def commit_edit(self):
        resp = requests.post(
            f'{API_BASE}/applications/{self.package_name}/edits/{self.edit_id}:commit',
            headers=self.headers()
        )
        resp.raise_for_status()
        print('Edit committed successfully.')

    def delete_edit(self):
        requests.delete(
            f'{API_BASE}/applications/{self.package_name}/edits/{self.edit_id}',
            headers=self.headers()
        )

    def upload_bundle(self, aab_path):
        print(f'Uploading AAB: {aab_path}')
        with open(aab_path, 'rb') as f:
            resp = requests.post(
                f'{UPLOAD_BASE}/applications/{self.package_name}/edits/{self.edit_id}/bundles?uploadType=media',
                headers={**self.headers(), 'Content-Type': 'application/octet-stream'},
                data=f
            )
        resp.raise_for_status()
        info = resp.json()
        print(f'Uploaded AAB — versionCode: {info["versionCode"]}')
        return info['versionCode']

    def set_track(self, track, version_codes, status='completed', fraction=None, release_notes=None):
        release = {
            'versionCodes': [str(vc) for vc in version_codes],
            'status': status,
        }
        if fraction is not None and status == 'inProgress':
            release['userFraction'] = fraction
        if release_notes:
            release['releaseNotes'] = release_notes

        resp = requests.put(
            f'{API_BASE}/applications/{self.package_name}/edits/{self.edit_id}/tracks/{track}',
            headers={**self.headers(), 'Content-Type': 'application/json'},
            json={'track': track, 'releases': [release]}
        )
        resp.raise_for_status()
        print(f'Track "{track}" updated — status: {status}, versionCodes: {version_codes}')

    def update_listing(self, language, title=None, short_desc=None, full_desc=None):
        data = {'language': language}
        if title:
            data['title'] = title[:50]
        if short_desc:
            data['shortDescription'] = short_desc[:80]
        if full_desc:
            data['fullDescription'] = full_desc[:4000]

        resp = requests.put(
            f'{API_BASE}/applications/{self.package_name}/edits/{self.edit_id}/listings/{language}',
            headers={**self.headers(), 'Content-Type': 'application/json'},
            json=data
        )
        resp.raise_for_status()
        print(f'Listing updated for {language}')

    def delete_images(self, language, image_type):
        resp = requests.delete(
            f'{API_BASE}/applications/{self.package_name}/edits/{self.edit_id}/listings/{language}/{image_type}',
            headers=self.headers()
        )
        if resp.status_code == 200:
            print(f'Deleted existing {image_type} for {language}')

    def upload_image(self, language, image_type, image_path):
        content_type = 'image/png' if image_path.endswith('.png') else 'image/jpeg'
        with open(image_path, 'rb') as f:
            resp = requests.post(
                f'{UPLOAD_BASE}/applications/{self.package_name}/edits/{self.edit_id}/listings/{language}/{image_type}?uploadType=media',
                headers={**self.headers(), 'Content-Type': content_type},
                data=f
            )
        resp.raise_for_status()
        print(f'Uploaded {os.path.basename(image_path)} → {image_type} ({language})')

    def upload_screenshots_from_dir(self, metadata_dir):
        android_dir = os.path.join(metadata_dir, 'android')
        if not os.path.isdir(android_dir):
            print(f'No android metadata dir found at {android_dir}')
            return

        for locale in sorted(os.listdir(android_dir)):
            locale_dir = os.path.join(android_dir, locale)
            if not os.path.isdir(locale_dir):
                continue
            images_dir = os.path.join(locale_dir, 'images')
            if not os.path.isdir(images_dir):
                continue

            for img_type in IMAGE_TYPES:
                type_dir = os.path.join(images_dir, img_type)
                if not os.path.isdir(type_dir):
                    continue
                files = sorted(glob.glob(os.path.join(type_dir, '*.png')) +
                              glob.glob(os.path.join(type_dir, '*.jpg')) +
                              glob.glob(os.path.join(type_dir, '*.jpeg')))
                if not files:
                    continue
                self.delete_images(locale, img_type)
                for f in files[:8]:
                    self.upload_image(locale, img_type, f)

            for asset_type in ASSET_TYPES:
                for ext in ['png', 'jpg', 'jpeg']:
                    asset_path = os.path.join(images_dir, f'{asset_type}.{ext}')
                    if os.path.exists(asset_path):
                        self.delete_images(locale, asset_type)
                        self.upload_image(locale, asset_type, asset_path)
                        break

    def upload_metadata_from_dir(self, metadata_dir):
        android_dir = os.path.join(metadata_dir, 'android')
        if not os.path.isdir(android_dir):
            print(f'No android metadata dir found at {android_dir}')
            return

        for locale in sorted(os.listdir(android_dir)):
            locale_dir = os.path.join(android_dir, locale)
            if not os.path.isdir(locale_dir):
                continue

            title = self._read_file(os.path.join(locale_dir, 'title.txt'))
            short_desc = self._read_file(os.path.join(locale_dir, 'short_description.txt'))
            full_desc = self._read_file(os.path.join(locale_dir, 'full_description.txt'))

            if title or short_desc or full_desc:
                self.update_listing(locale, title, short_desc, full_desc)

    def get_tracks(self):
        resp = requests.get(
            f'{API_BASE}/applications/{self.package_name}/edits/{self.edit_id}/tracks',
            headers=self.headers()
        )
        resp.raise_for_status()
        return resp.json().get('tracks', [])

    @staticmethod
    def _read_file(path):
        if os.path.exists(path):
            return open(path, 'r').read().strip()
        return None


def main():
    parser = argparse.ArgumentParser(description='Google Play Store Uploader')
    parser.add_argument('action', choices=[
        'screenshots', 'metadata', 'upload-aab', 'upload-apk',
        'release', 'all', 'status', 'rollout'
    ])
    parser.add_argument('--package', required=True, help='App package name')
    parser.add_argument('--metadata-dir', default='fastlane/metadata', help='Metadata directory')
    parser.add_argument('--aab', help='Path to AAB file')
    parser.add_argument('--apk', help='Path to APK file')
    parser.add_argument('--track', default='internal', help='Track: internal/alpha/beta/production')
    parser.add_argument('--version-code', type=int, help='Version code for release')
    parser.add_argument('--status', default='completed', help='Release status')
    parser.add_argument('--fraction', type=float, help='Rollout fraction (0.0-1.0)')
    parser.add_argument('--notes', help='Release notes text')
    args = parser.parse_args()

    uploader = GooglePlayUploader(args.package)

    if args.action == 'status':
        uploader.authenticate()
        uploader.create_edit()
        tracks = uploader.get_tracks()
        for t in tracks:
            print(f"\nTrack: {t['track']}")
            for r in t.get('releases', []):
                print(f"  Status: {r.get('status')} | Versions: {r.get('versionCodes', [])}")
                if 'userFraction' in r:
                    print(f"  Rollout: {r['userFraction']*100:.0f}%")
        uploader.delete_edit()
        return

    uploader.authenticate()
    uploader.create_edit()

    try:
        if args.action in ('metadata', 'all'):
            uploader.upload_metadata_from_dir(args.metadata_dir)

        if args.action in ('screenshots', 'all'):
            uploader.upload_screenshots_from_dir(args.metadata_dir)

        if args.action == 'upload-aab':
            if not args.aab:
                print('ERROR: --aab path required')
                sys.exit(1)
            vc = uploader.upload_bundle(args.aab)
            print(f'Bundle uploaded. Version code: {vc}')

        if args.action == 'release':
            if not args.version_code:
                print('ERROR: --version-code required for release')
                sys.exit(1)
            release_notes = None
            if args.notes:
                release_notes = [{'language': 'en-US', 'text': args.notes}]
            uploader.set_track(
                args.track,
                [args.version_code],
                status=args.status,
                fraction=args.fraction,
                release_notes=release_notes
            )

        if args.action == 'rollout':
            if not args.version_code or args.fraction is None:
                print('ERROR: --version-code and --fraction required')
                sys.exit(1)
            uploader.set_track(
                args.track,
                [args.version_code],
                status='inProgress',
                fraction=args.fraction
            )

        uploader.commit_edit()
        print('\n✅ All done!')

    except Exception as e:
        print(f'\n❌ Error: {e}')
        import traceback
        traceback.print_exc()
        uploader.delete_edit()
        sys.exit(1)


if __name__ == '__main__':
    main()
