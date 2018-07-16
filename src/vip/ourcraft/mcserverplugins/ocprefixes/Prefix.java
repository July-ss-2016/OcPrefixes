package vip.ourcraft.mcserverplugins.ocprefixes;

import java.util.Objects;

public class Prefix {
    private String prefixName;
    private long expiredTime;
    private boolean isDefaultPrefix;

    public Prefix() {
    }

    public Prefix(String prefixName, long expiredTime) {
        this.prefixName = prefixName;
        this.expiredTime = expiredTime;
    }

    public Prefix(String prefixName, long expiredTime, boolean isDefaultPrefix) {
        this.prefixName = prefixName;
        this.expiredTime = expiredTime;
        this.isDefaultPrefix = isDefaultPrefix;
    }

    public String getPrefixName() {
        return prefixName;
    }

    public void setPrefixName(String prefixName) {
        this.prefixName = prefixName;
    }

    public long getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(long expiredTime) {
        this.expiredTime = expiredTime;
    }

    public boolean isDefaultPrefix() {
        return isDefaultPrefix;
    }

    public void setDefaultPrefix(boolean defaultPrefix) {
        isDefaultPrefix = defaultPrefix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prefix prefix = (Prefix) o;
        return prefixName != null && prefixName.equals(prefix.getPrefixName()) && expiredTime == prefix.getExpiredTime();
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefixName, expiredTime);
    }
}
