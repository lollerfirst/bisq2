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

package bisq.account.protocol_type;

import bisq.common.util.ProtobufUtils;

public enum TradeProtocolType implements ProtocolType {
    BISQ_EASY,
    MULTISIG,
    SUBMARINE,
    MULTISIG_LIQUID,
    LIQUID_SWAP,
    BSQ_SWAP,
    LIGHTNING_X,
    MONERO_SWAP;

    @Override
    public bisq.account.protobuf.TradeProtocolType toProto() {
        return bisq.account.protobuf.TradeProtocolType.valueOf(name());
    }

    public static TradeProtocolType fromProto(bisq.account.protobuf.TradeProtocolType proto) {
        return ProtobufUtils.enumFromProto(TradeProtocolType.class, proto.name());
    }
}