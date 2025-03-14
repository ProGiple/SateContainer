package org.satellite.dev.progiple.satecontainer.event;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ContainerManager {
    @Getter @Setter private ContainerEvent event;

    public void startEvent() {
        if (event != null) return;
        new ContainerEvent();
    }

    public void end() {
        if (event == null) return;
        event.end(false);
    }
}
