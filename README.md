AuthenticatorPlus
=================

About
-----
This project is a fork of the Google Authenticator for Android project. This fork adds a **MAJOR SECURITY VULNERABILITY** and should not be used by anyone. _You've been warned!_
Using this app will allow you to export your OTP secret tokens and import them into the app on another device.

Overview
--------
The Google Authenticator project includes implementations of one-time passcode generators for several mobile platforms, as well as a pluggable authentication module (PAM). One-time passcodes are generated using open standards developed by the Initiative for Open Authentication (OATH) (which is unrelated to OAuth).

These implementations support the HMAC-Based One-time Password (HOTP) algorithm specified in RFC 4226 and the Time-based One-time Password (TOTP) algorithm specified in RFC 6238.

Google Authenticator for Android
--------------------------------
The Android mobile app supports:

* Multiple accounts
* Support for 30-second TOTP codes
* Support for counter-based HOTP codes
* Key provisioning via scanning a QR code
* Manual key entry of RFC 3548 base32 key strings
* DISCLAIMER: This open source project allows you to download the code that powered version 2.21 of the application. Subsequent versions contain Google-specific workflows that are not part of the project.