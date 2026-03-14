package com.comercialvalerio.application.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/*
 * Activa JAX-RS en WildFly:
 * - Escanea todo @Path y @Provider
 * - Permite usar @Valid en los recursos
 */
@ApplicationPath("/api")
public class JaxRsActivator extends Application { }
