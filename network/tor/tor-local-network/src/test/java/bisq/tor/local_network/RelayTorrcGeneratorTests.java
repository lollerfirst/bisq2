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

import bisq.tor.local_network.torrc.RelayTorrcGenerator;
import bisq.tor.local_network.torrc.TestNetworkTorrcGenerator;
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

public class RelayTorrcGeneratorTests {
    @Test
    void basicTest(@TempDir Path tempDir) throws IOException {
        Path relayAPath = tempDir.resolve("RELAY_A");
        assertThat(relayAPath.toFile().mkdir()).isTrue();

        TorNode firstRelay = spy(
                TorNode.builder()
                        .type(TorNode.Type.RELAY)
                        .nickname("A")
                        .dataDir(relayAPath)

                        .controlPort(1)
                        .orPort(2)
                        .dirPort(3)

                        .build()
        );

        doReturn(Optional.of("AAAA_fp"))
                .when(firstRelay)
                .getAuthorityIdentityKeyFingerprint();

        doReturn(Optional.of("AAAA_v3"))
                .when(firstRelay)
                .getRelayKeyFingerprint();

        TorNode secondRelay = spy(
                TorNode.builder()
                        .type(TorNode.Type.RELAY)
                        .nickname("B")
                        .dataDir(tempDir.resolve("DA_B"))

                        .controlPort(1)
                        .orPort(2)
                        .dirPort(3)

                        .build()
        );

        doReturn(Optional.of("BBBB_fp"))
                .when(secondRelay)
                .getAuthorityIdentityKeyFingerprint();

        doReturn(Optional.of("BBBB_v3"))
                .when(secondRelay)
                .getRelayKeyFingerprint();

        var testNetworkTorrcGenerator = new TestNetworkTorrcGenerator(firstRelay);
        var relayTorrcGenerator = new RelayTorrcGenerator(testNetworkTorrcGenerator);
        var allDirAuthorities = Set.of(firstRelay, secondRelay);

        Map<String, String> torrcConfigs = relayTorrcGenerator.generate();
        Path torrcPath = firstRelay.getTorrcPath();
        var torrcFileGenerator = new TorrcFileGenerator(torrcPath, torrcConfigs , allDirAuthorities);
        torrcFileGenerator.generate();

        assertThat(firstRelay.getTorrcPath())
                .isNotEmptyFile();
    }
}