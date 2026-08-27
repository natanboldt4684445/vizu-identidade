package com.vizu.identidade.shared.enums;

public enum ActivationAction {
    DESATIVAR, REATIVAR;

    public boolean active() {
        return this == REATIVAR;
    }
}
