package com.marginallyclever.makelangelo;

import com.marginallyclever.util.Ancestryable;
import com.marginallyclever.util.MarginallyCleverJsonFilePreferencesFactory;
import org.json.JSONObject;
import org.json.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Created on 5/17/15. FIXME Write Javadocs.
 *
 * @author Peter Colapietro
 * @since v7.1.4
 */
public final class PreferencesHelper {

    /**
     *
     */
    private static final Logger logger = LoggerFactory.getLogger(PreferencesHelper.class);

    /**
     *
     */
    private static final Map<MakelangeloPreferenceKey, ? super Preferences> CLASS_TO_PREFERENCE_NODE_MAP;

    /**
     * @see <a href="http://stackoverflow.com/a/507658">How can I Initialize a static Map?</a>
     */
    static {
        final Map<MakelangeloPreferenceKey, ? super Preferences> initialMap = new HashMap<>();
        final Preferences userRootPreferencesNode = Preferences.userRoot();
        //final String thisPackageName = PreferencesHelper.class.getPackage().getName();
        final Preferences makelangeloPreferenceNode = userRootPreferencesNode.node("DrawBot");// thisPackageName); FIXME write unit test/tool to view import/export machine configurations.
        initialMap.put(MakelangeloPreferenceKey.MAKELANGELO_ROOT, makelangeloPreferenceNode);
        initialMap.put(MakelangeloPreferenceKey.GRAPHICS, makelangeloPreferenceNode.node("Graphics"));
        initialMap.put(MakelangeloPreferenceKey.MACHINES, makelangeloPreferenceNode.node("Machines"));
        initialMap.put(MakelangeloPreferenceKey.LANGUAGE, makelangeloPreferenceNode.node("Language"));
        CLASS_TO_PREFERENCE_NODE_MAP = Collections.unmodifiableMap((Map<? extends MakelangeloPreferenceKey, ? extends Preferences>)initialMap);
    }

    /**
     * NOOP Constructor.
     *
     * @throws IllegalStateException
     */
    private PreferencesHelper() throws IllegalStateException { throw new IllegalStateException(); }

    /**
     *
     * @param key enumeration key used to look up a Makelangelo preference value.
     * @return
     */
    public static <P extends Preferences> P getPreferenceNode(MakelangeloPreferenceKey key) {
        return (P) CLASS_TO_PREFERENCE_NODE_MAP.get(key);
    }

    public enum MakelangeloPreferenceKey {
        GRAPHICS,
        MACHINES,
        LANGUAGE,
        MAKELANGELO_ROOT
    }

    /**
     *
     * @param preferenceNode Preference node whose name, and key values,
     *                       as well as those of its children's are to be logged.
     */
    public static <P extends Preferences> void logPreferenceNode(P preferenceNode) {
        try {
            logger.info("node name:{}", preferenceNode);
            logKeyValuesForPreferenceNode(preferenceNode);
            final String[] childrenPreferenceNodeNames = preferenceNode.childrenNames();
            for (String childNodeName : childrenPreferenceNodeNames) {
                final Preferences childNode = preferenceNode.node(childNodeName);
                logPreferenceNode(childNode);
            }
        } catch (BackingStoreException e) {
            logger.error("{}",e);
        }
    }


  /**
   *
   * @param preferenceNode Preference node to log key value pairs for.
   */
  public static <P extends Preferences> void logKeyValuesForPreferenceNode(P preferenceNode) throws BackingStoreException {
    final String[] keys = preferenceNode.keys();
    for (String key : keys) {
      logger.info("key:{} value:{}", key, preferenceNode.get(key, null));
    }
  }


  /**
   *
   * @param sourcePreferenceNode Preference node to be copied from.
   * @param destinationPreferenceNode Preference node to be copied to.
   */
  public static <P extends Preferences> void copyPreferenceNode(P sourcePreferenceNode, P destinationPreferenceNode) {
    try {
      final String[] keys = sourcePreferenceNode.keys();
      for (String key: keys) {
        final String value = sourcePreferenceNode.get(key, null);
        destinationPreferenceNode.put(key, value);
      }
      final String[] childNames = sourcePreferenceNode.childrenNames();
      for (String childName: childNames) {
        final Preferences destinationChildNode = destinationPreferenceNode.node(childName);
        copyPreferenceNode(sourcePreferenceNode.node(childName), destinationChildNode);
      }
    } catch (BackingStoreException e) {
      logger.error("{}", e);
    }
  }


  /**
   *
   * Recursively clears all the preferences (key-value associations) for a given node and its children.
   *
   * @param preferenceNode Preference node that you want recursively cleared of all key value pairs.
   *
   * @see <a href="http://stackoverflow.com/a/6411855"></a>
   */
  public static <P extends Preferences> void clearAll(P preferenceNode) throws BackingStoreException {
    final String[] childrenNames = preferenceNode.childrenNames();
    for(String childNodeName : childrenNames) {
      final Preferences childNode = preferenceNode.node(childNodeName);
      final String[] childNodesChildren = childNode.childrenNames();
      if(childNodesChildren != null) {
        final boolean hasChildren = childNodesChildren.length != 0;
        if(hasChildren) {
          clearAll(childNode);
        }
        childNode.clear();
      }
    }
    preferenceNode.clear();
  }

  /**
   * Removes all of the preferences (key-value associations) in this
   * preference node with no effect on any descendants
   * of this node.
   */
  public static <P extends Preferences> void shallowClearPreferences(P preferenceNode) {
    try {
      preferenceNode.clear();
    } catch (BackingStoreException e) {
      logger.error("{}", e);
    }
  }

  /**
   * Removes all of the preferences (key-value associations) in this
   * preference node and any descendants of this node.
   */
  public static <P extends Preferences> void deepClearPreferences(P preferenceNode) {
    try {
      preferenceNode.clear();
      final String[] childrenPreferenceNodeNames = preferenceNode.childrenNames();
      for (String childNodeName : childrenPreferenceNodeNames) {
        final Preferences childNode = preferenceNode.node(childNodeName);
        childNode.clear();
      }
    } catch (BackingStoreException e) {
      logger.error("{}", e);
    }
  }

  /**
   *
   */
  public  <P extends Preferences> void testCopyPreferenceNode(P marginallyCleverJsonPreferenceNode, P preferenceNode) {
    try {
      PreferencesHelper.clearAll(marginallyCleverJsonPreferenceNode);
    } catch (BackingStoreException e) {
      logger.error("{}", e.getMessage());
    }
    PreferencesHelper.copyPreferenceNode(preferenceNode, marginallyCleverJsonPreferenceNode);
    final File preferencesFile = MarginallyCleverJsonFilePreferencesFactory.getPreferencesFile();
    final Properties p = new Properties();
    try(final FileInputStream inStream = new FileInputStream(preferencesFile)) {
      p.load(inStream);
    } catch (IOException e) {
      logger.error("{}", e.getMessage());
    }
    logPropertiesNode(p);
    //logAncestryable(marginallyCleverJsonPreferenceNode);
  }

  /**
   *
   * @param marginallyCleverJsonPreferenceNode
   */
  public static <P extends Preferences> void logAncestryable(Ancestryable<P> marginallyCleverJsonPreferenceNode) {
    final JSONObject object = new JSONObject(marginallyCleverJsonPreferenceNode.getChildren());
    logger.debug("{}", object);
  }

  /**
   *
   * @param p
   */
  public static <P extends Properties> void logPropertiesNode(P p) {
    final JSONObject jsonObject = Property.toJSONObject(p);
    logger.debug("{}", jsonObject);
  }

}
