module be.yildizgames.authentication {

    requires be.yildizgames.common.logging;
    requires be.yildizgames.common.configuration;
    requires be.yildizgames.common.configuration.parameter;
    requires be.yildizgames.common.exception;
    requires be.yildizgames.module.database.derby;
    requires be.yildizgames.module.database;
    requires be.yildizgames.module.messaging;
    requires java.sql;
    requires be.yildizgames.common.authentication;
    requires be.yildizgames.common.model;
    requires be.yildizgames.common.util;
}