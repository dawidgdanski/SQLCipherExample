SQLCipherExample
================

Example app using encrypted storage (SQLCipher from Zetetic).

If you decide to use SQLCipher, be aware of following facts:

- your .apk weighs ~ 4MB more (compared with the same App using standard android.database.sqlite components).
- Retaining your database access key.
- (optionally) gradle requires Android NDK set up in order to build your app.

For more information and sources refer to:
- http://sqlcipher.net/open-source
- http://sqlcipher.net/sqlcipher-api/
- https://github.com/commonsguy/cw-omnibus/tree/master/Database