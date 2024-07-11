package bisq.gradle.tor_binary

import bisq.gradle.tasks.PerOsUrlProvider

class TorBinaryUrlProvider(private val version: String) : PerOsUrlProvider {
    override val urlPrefix: String
        get() = "https://archive.torproject.org/tor-package-archive/torbrowser/$version/"

    override val linuxUrl: String
        get() = "tor-expert-bundle-$version-linux-x86_64.tar.gz"

    override val macOsUrl: String
        get() = "tor-expert-bundle-$version-macos-x86_64.tar.gz"

    // Tor binary for macOsAarch64 does not work yet, so we use still the x86 version
    /* override val macOsAarch64Url: String
         get() = "tor-expert-bundle-$version-macos-aarch64.tar.gz"*/
    override val macOsAarch64Url: String
        get() = "tor-expert-bundle-$version-macos-x86_64.tar.gz"

    override val windowsUrl: String
        get() = "tor-expert-bundle-$version-windows-x86_64.tar.gz"
}