package org.javers.core.diff.appenders;

import org.javers.common.collections.Lists;
import org.javers.core.diff.changetype.container.ListChange;
import org.javers.core.pico.InstantiatingModule;
import org.picocontainer.MutablePicoContainer;

import java.util.Collection;

/**
 * @author bartosz walacik
 */
public class DiffAppendersModule extends InstantiatingModule {

    private Class<? extends CorePropertyChangeAppender<ListChange>> listChangeAppenderClass = ListChangeAppender.class;

    public DiffAppendersModule(MutablePicoContainer container,
                               Class<? extends CorePropertyChangeAppender<ListChange>> listChangeAppenderClass) {
        super(container);
        this.listChangeAppenderClass = listChangeAppenderClass;
    }

    @Override
    protected Collection<Class> getImplementations() {

        return (Collection)Lists.asList(
                NewObjectAppender.class,
                MapChangeAppender.class,
                listChangeAppenderClass,
                SetChangeAppender.class,
                ArrayChangeAppender.class,
                ObjectRemovedAppender.class,
                ReferenceChangeAppender.class,
                ValueChangeAppender.class
        );
    }
}
