/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.tor.local_network;

import bisq.tor.local_network.torrc.DirectoryAuthorityTorrcGenerator;
import bisq.tor.local_network.torrc.TorrcFileGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class DirectoryAuthorityTorrcGeneratorTests {
    @Test
    void basicTest(@TempDir Path tempDir) throws IOException {
        Path daAPath = tempDir.resolve("DA_A");
        assertThat(daAPath.toFile().mkdir()).isTrue();

        TorNode firstDirAuth = spy(
                TorNode.builder()
                        .type(TorNode.Type.DIRECTORY_AUTHORITY)
                        .nickname("A")
                        .dataDir(daAPath)

                        .controlPort(1)
                        .orPort(2)
                        .dirPort(3)

                        .build()
        );

        doReturn(Optional.of("AAAA_fp"))
                .when(firstDirAuth)
                .getAuthorityIdentityKeyFingerprint();

        doReturn(Optional.of("AAAA_v3"))
                .when(firstDirAuth)
                .getRelayKeyFingerprint();

        TorNode secondDirAuth = spy(
                TorNode.builder()
                        .type(TorNode.Type.DIRECTORY_AUTHORITY)
                        .nickname("B")
                        .dataDir(tempDir.resolve("DA_B"))

                        .controlPort(1)
                        .orPort(2)
                        .dirPort(3)

                        .build()
        );

        doReturn(Optional.of("BBBB_fp"))
                .when(secondDirAuth)
                .getAuthorityIdentityKeyFingerprint();

        doReturn(Optional.of("BBBB_v3"))
                .when(secondDirAuth)
                .getRelayKeyFingerprint();

        var torDaTorrcGenerator = new DirectoryAuthorityTorrcGenerator(firstDirAuth);
        var allDirAuthorities = Set.of(firstDirAuth, secondDirAuth);

        Map<String, String> torrcConfigs = torDaTorrcGenerator.generate();
        Path torrcPath = firstDirAuth.getTorrcPath();
        var torrcFileGenerator = new TorrcFileGenerator(torrcPath, torrcConfigs , allDirAuthorities);

        torrcFileGenerator.generate();

        assertThat(firstDirAuth.getTorrcPath())
                .isNotEmptyFile();
    }
}