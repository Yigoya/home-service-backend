package com.home.service.models.enums;

public enum EthiopianLanguage {
    AMHARIC("AM"),
    OROMO("OM"),
    TIGRINYA("TI"),
    SOMALI("SO"),
    AFAR("AF"),
    SIDAMA("SD"),
    WOLAYTTA("WO"),
    GURAGE("GU"),
    ENGLISH("EN");

    private final String code;

    EthiopianLanguage(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
