public class KeyIdentifiers {
    private String performerId;
    private String performerAgencyId;
    private String performerType;
    private String performerRole;
    private String performerPrivilege;

    // Constructors, getters, setters...

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeyIdentifiers)) return false;
        KeyIdentifiers that = (KeyIdentifiers) o;
        return performerId.equals(that.performerId) &&
                performerAgencyId.equals(that.performerAgencyId) &&
                performerType.equals(that.performerType) &&
                performerRole.equals(that.performerRole) &&
                performerPrivilege.equals(that.performerPrivilege);
    }

    @Override
    public int hashCode() {
        return Objects.hash(performerId, performerAgencyId, performerType, performerRole, performerPrivilege);
    }
}
