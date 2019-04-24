module be.yildizgames.authentication {

    requires be.yildizgames.common.logging;
    requires be.yildizgames.common.configuration;
    requires be.yildizgames.common.exception;
    requires be.yildizgames.module.database.derby;
    requires be.yildizgames.module.database;
    requires be.yildizgames.module.messaging;
    requires be.yildizgames.common.authentication;
    requires be.yildizgames.common.model;
    requires be.yildizgames.common.util;
    requires be.yildizgames.common.application;
    
    requires java.sql;
    requires java.mail;
}
