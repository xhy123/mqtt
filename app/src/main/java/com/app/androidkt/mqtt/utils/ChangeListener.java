package com.app.androidkt.mqtt.utils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created by xhy on 2018/6/26 0026.
 *
 * @author xhy
 * @data 2018/6/26 0026
 */

public class ChangeListener implements PropertyChangeListener {
    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {

        if (!event.getPropertyName().equals(ActivityConstants.ConnectionStatusProperty)) {
            return;
        }


    }
}
