package com.campos.david.appointments.model;

import android.net.Uri;

/**
 * Created by David Campos Rodríguez <a href='mailto:david.campos@rai.usc.es'>david.campos@rai.usc.es</a>
 */
public class DBHelper {
    // Usado por el ContentProvider, debe ser único en el dispositivo, por lo que se aconseja usar
    // el nombre de paquete de la app
    public static final String CONTENT_AUTHORITY = "com.campos.david.appointments";
    // Uri base para obtener contenido en esta app
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
}
