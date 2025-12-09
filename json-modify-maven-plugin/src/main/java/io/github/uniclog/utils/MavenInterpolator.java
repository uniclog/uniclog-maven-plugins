package io.github.uniclog.utils;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.interpolation.*;

import java.util.Map;
import java.util.Properties;

public class MavenInterpolator {

    private final MavenProject project;
    private final MavenSession session;

    public MavenInterpolator(MavenProject project, MavenSession session) {
        this.project = project;
        this.session = session;
    }

    public String interpolate(String text) {
        if (text == null) {
            return null;
        }
        StringSearchInterpolator interpolator = new StringSearchInterpolator();
        interpolator.addValueSource(new PrefixedObjectValueSource("project.", project));
        interpolator.addValueSource(new PrefixedObjectValueSource("pom.", project));
        interpolator.addValueSource(new MapBasedValueSource(Map.of("basedir", project.getBasedir().getAbsolutePath())));
        interpolator.addValueSource(new MapBasedValueSource(session.getUserProperties()));
        Properties sysProps = new Properties();
        sysProps.putAll(session.getSystemProperties());
        sysProps.putAll(System.getProperties());
        interpolator.addValueSource(new MapBasedValueSource(sysProps));
        interpolator.addValueSource(new PrefixedValueSourceWrapper(new MapBasedValueSource(System.getenv()), "env."));
        if (session.getSettings() != null) {
            interpolator.addValueSource(new PrefixedObjectValueSource("settings.", session.getSettings()));
        }
        try {
            return interpolator.interpolate(text);
        } catch (InterpolationException e) {
            return text;
        }
    }
}
