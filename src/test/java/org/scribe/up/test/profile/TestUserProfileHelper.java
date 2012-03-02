/*
  Copyright 2012 Jerome Leleu

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.scribe.up.test.profile;

import junit.framework.TestCase;

import org.codehaus.jackson.JsonNode;
import org.scribe.up.profile.JsonHelper;
import org.scribe.up.profile.UserProfile;
import org.scribe.up.profile.UserProfileHelper;
import org.scribe.up.test.util.MockAttributeConverter;

/**
 * This class tests the UserProfileHelper class.
 * 
 * @author Jerome Leleu
 * @since 1.0.0
 */
public final class TestUserProfileHelper extends TestCase {
    
    private static final String ID = "id";
    
    private static final String KEY = "key";
    
    private static final String VALUE = "value";
    
    private static final String GOOD_JSON = "{ \"" + KEY + "\" : \"" + VALUE + "\" }";
    
    public void testAddIdentifier() {
        UserProfile userProfile = new UserProfile();
        assertNull(userProfile.getId());
        UserProfileHelper.addIdentifier(userProfile, ID);
        assertEquals(ID, userProfile.getId());
    }
    
    public void testAddIdentifierJson() {
        UserProfile userProfile = new UserProfile();
        JsonNode json = JsonHelper.getFirstNode(GOOD_JSON);
        UserProfileHelper.addIdentifier(userProfile, json, KEY);
        assertEquals(VALUE, userProfile.getId());
    }
    
    public void testAddAttribute() {
        UserProfile userProfile = new UserProfile();
        assertNull(userProfile.getAttributes().get(KEY));
        UserProfileHelper.addAttribute(userProfile, KEY, VALUE);
        assertEquals(VALUE, userProfile.getAttributes().get(KEY));
    }
    
    public void testAddAttributeConversion() {
        UserProfile userProfile = new UserProfile();
        assertNull(userProfile.getAttributes().get(KEY));
        UserProfileHelper.addAttribute(userProfile, KEY, VALUE, new MockAttributeConverter());
        assertEquals(MockAttributeConverter.CONVERTED_VALUE, userProfile.getAttributes().get(KEY));
    }
    
    public void testAddAttributeJson() {
        UserProfile userProfile = new UserProfile();
        JsonNode json = JsonHelper.getFirstNode(GOOD_JSON);
        UserProfileHelper.addAttribute(userProfile, json, KEY);
        assertEquals(VALUE, userProfile.getAttributes().get(KEY));
    }
    
    public void testAddAttributeJsonConversion() {
        UserProfile userProfile = new UserProfile();
        JsonNode json = JsonHelper.getFirstNode(GOOD_JSON);
        UserProfileHelper.addAttribute(userProfile, json, KEY, new MockAttributeConverter());
        assertEquals(MockAttributeConverter.CONVERTED_VALUE, userProfile.getAttributes().get(KEY));
    }
}
