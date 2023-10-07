package org.apache.hadoop.hdfs.server.federation.router;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.security.token.Token;
import org.apache.hadoop.security.token.TokenIdentifier;
import org.apache.hadoop.thirdparty.com.google.common.annotations.VisibleForTesting;

import java.util.*;

// 用户到 NameNode连接的 唯一标识符
@InterfaceAudience.Private
@InterfaceStability.Evolving
public class ConnectionPoolId implements Comparable<ConnectionPoolId> {

    private final String nnId;
    private final UserGroupInformation ugi;
    private final Class<?> protocol;
    private final String ugiString;

    public ConnectionPoolId(final UserGroupInformation ugi, final String nnId,
                            final Class<?> proto) {
        this.nnId = nnId;
        this.ugi = ugi;
        this.protocol = proto;
        this.ugiString = ugi.toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(this.nnId)
                .append(this.ugiString)
                .append(this.getTokenIds())
                .append(this.protocol)
                .toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ConnectionPoolId) {
            ConnectionPoolId other = (ConnectionPoolId) o;
            if (!this.nnId.equals(other.nnId)) {
                return false;
            }
            if (!this.ugiString.equals(other.ugiString)) {
                return false;
            }
            String thisTokens = this.getTokenIds().toString();
            String otherTokens = other.getTokenIds().toString();
            if (!thisTokens.equals(otherTokens)) {
                return false;
            }
            return this.protocol.equals(other.protocol);
        }
        return false;
    }

    @Override
    public String toString() {
        return this.ugi + " " + this.getTokenIds() + "->" + this.nnId + " [" +
                this.protocol.getSimpleName() + "]";
    }

    @Override
    public int compareTo(ConnectionPoolId other) {
        int ret = this.nnId.compareTo(other.nnId);
        if (ret == 0) {
            ret = this.ugi.toString().compareTo(other.ugi.toString());
        }
        if (ret == 0) {
            String thisTokens = this.getTokenIds().toString();
            String otherTokens = other.getTokenIds().toString();
            ret = thisTokens.compareTo(otherTokens);
        }
        if (ret == 0) {
            ret = this.protocol.toString().compareTo(other.protocol.toString());
        }
        return ret;
    }

    @VisibleForTesting
    UserGroupInformation getUgi() {
        return this.ugi;
    }

    private List<String> getTokenIds() {
        List<String> tokenIds = new ArrayList<>();
        Collection<Token<? extends TokenIdentifier>> tokens = this.ugi.getTokens();
        for (Token<? extends TokenIdentifier> token : tokens) {
            byte[] tokenIdBytes = token.getIdentifier();
            String tokenId = Arrays.toString(tokenIdBytes);
            tokenIds.add(tokenId);
        }
        Collections.sort(tokenIds);
        return tokenIds;
    }
}
