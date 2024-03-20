package ru.adel.catalogue.domain.constant;

public enum EndpointPermission {
    EDIT_CATALOGUE("SCOPE_edit_catalogue"),
    VIEW_CATALOGUE("SCOPE_view_catalogue");

    private final String authority;


    EndpointPermission(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return authority;
    }
}
