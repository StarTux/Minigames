package com.winthier.minigames.event;

import java.lang.reflect.Method;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;

/**
 * Call one EventHandler routine.
 */
public class HandlerCaller {
        private final Class<? extends Event> event;
        private final Listener listener;
        private final Method method;
        public final boolean ignoreCancelled;

        public HandlerCaller(Class<? extends Event> event, Listener listener, Method method, boolean ignoreCancelled) {
                this.event = event;
                this.listener = listener;
                this.method = method;
                this.ignoreCancelled = ignoreCancelled;
        }

        public void call(Event event) {
                try {
                        method.invoke(listener, this.event.cast(event));
                } catch (Throwable t) {
                        t.printStackTrace();
                }
        }
}
