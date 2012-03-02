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
package org.scribe.up.provider.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FacebookApi;
import org.scribe.up.profile.DateConverter;
import org.scribe.up.profile.GenderConverter;
import org.scribe.up.profile.JsonHelper;
import org.scribe.up.profile.UserProfile;
import org.scribe.up.profile.UserProfileHelper;
import org.scribe.up.profile.facebook.FacebookEducation;
import org.scribe.up.profile.facebook.FacebookObject;
import org.scribe.up.profile.facebook.FacebookProfile;
import org.scribe.up.profile.facebook.FacebookProfileHelper;
import org.scribe.up.profile.facebook.FacebookRelationshipStatusConverter;
import org.scribe.up.profile.facebook.FacebookWork;
import org.scribe.up.provider.BaseOAuth20Provider;
import org.scribe.up.util.StringHelper;

/**
 * This class is the OAuth provider to authenticate user in Facebook. Specific scopes and attributes are defined in
 * http://developers.facebook.com/docs/reference/api/user/.<br />
 * Attributes (Java type) available in {@link org.scribe.up.profile.facebook.FacebookProfile} : name (String), first_name (String),
 * middle_name (String), last_name (String), gender (Gender), locale (Locale), languages (List<FacebookObject>), link (String), username
 * (String), third_party_id (String), timezone (Integer), updated_time (Date), verified (Boolean), bio (String), birthday (Date), education
 * (List<FacebookEducation>), email (String), hometown (FacebookObject), interested_in (List<String>), location (FacebookObject), political
 * (String), favorite_athletes (List<FacebookObject>), favorite_teams (List<FacebookObject>), quotes (String), relationship_status
 * (FacebookRelationshipStatus), religion (String), significant_other (FacebookObject), website (String), work (List<FacebookWork>).
 * 
 * @author Jerome Leleu
 * @since 1.0.0
 */
public class FacebookProvider extends BaseOAuth20Provider {
    
    @Override
    protected void internalInit() {
        if (StringHelper.isNotBlank(scope)) {
            service = new ServiceBuilder().provider(FacebookApi.class).apiKey(key).apiSecret(secret)
                .callback(callbackUrl).scope(scope).build();
        } else {
            service = new ServiceBuilder().provider(FacebookApi.class).apiKey(key).apiSecret(secret)
                .callback(callbackUrl).build();
        }
        String[] names = new String[] {
            FacebookProfile.NAME, FacebookProfile.FIRST_NAME, FacebookProfile.MIDDLE_NAME, FacebookProfile.LAST_NAME,
            FacebookProfile.LINK, FacebookProfile.USERNAME, FacebookProfile.THIRD_PARTY_ID, FacebookProfile.TIMEZONE,
            FacebookProfile.VERIFIED, FacebookProfile.BIO, FacebookProfile.EMAIL, FacebookProfile.POLITICAL,
            FacebookProfile.QUOTES, FacebookProfile.RELIGION, FacebookProfile.WEBSITE
        };
        for (String name : names) {
            mainAttributes.put(name, null);
        }
        mainAttributes.put(FacebookProfile.GENDER, new GenderConverter("male"));
        mainAttributes.put(FacebookProfile.LOCALE, localeConverter);
        mainAttributes.put(FacebookProfile.UPDATED_TIME, new DateConverter("yyyy-MM-dd'T'HH:mm:ssz"));
        mainAttributes.put(FacebookProfile.BIRTHDAY, new DateConverter("MM/dd/yyyy"));
        mainAttributes.put(FacebookProfile.RELATIONSHIP_STATUS, new FacebookRelationshipStatusConverter());
    }
    
    @Override
    protected String getProfileUrl() {
        return "https://graph.facebook.com/me";
    }
    
    @Override
    protected UserProfile extractUserProfile(String body) {
        FacebookProfile profile = new FacebookProfile();
        JsonNode json = JsonHelper.getFirstNode(body);
        if (json != null) {
            UserProfileHelper.addIdentifier(profile, json, FacebookProfile.ID);
            for (String attribute : mainAttributes.keySet()) {
                UserProfileHelper.addAttribute(profile, json, attribute, mainAttributes.get(attribute));
            }
        }
        // languages
        JsonNode subJson = json.get(FacebookProfile.LANGUAGES);
        if (subJson != null) {
            UserProfileHelper.addAttribute(profile, FacebookProfile.LANGUAGES,
                                           FacebookProfileHelper.getListFacebookObject(subJson));
        }
        // installed
        // education
        subJson = json.get(FacebookProfile.EDUCATION);
        if (subJson != null) {
            List<FacebookEducation> education = new ArrayList<FacebookEducation>();
            Iterator<JsonNode> educationIterator = subJson.getElements();
            while (educationIterator.hasNext()) {
                JsonNode jsonEducation = educationIterator.next();
                FacebookEducation oneEducation = new FacebookEducation(jsonEducation);
                education.add(oneEducation);
            }
            UserProfileHelper.addAttribute(profile, FacebookProfile.EDUCATION, education);
        }
        // hometown
        subJson = json.get(FacebookProfile.HOMETOWN);
        if (subJson != null) {
            UserProfileHelper.addAttribute(profile, FacebookProfile.HOMETOWN, new FacebookObject(subJson));
        }
        // interested_in
        subJson = json.get(FacebookProfile.INTERESTED_IN);
        if (subJson != null) {
            Iterator<JsonNode> interestIterator = subJson.getElements();
            List<String> interested_in = new ArrayList<String>();
            while (interestIterator.hasNext()) {
                JsonNode interest = interestIterator.next();
                interested_in.add(interest.getTextValue());
            }
            UserProfileHelper.addAttribute(profile, FacebookProfile.INTERESTED_IN, interested_in);
        }
        // location
        subJson = json.get(FacebookProfile.LOCATION);
        if (subJson != null) {
            UserProfileHelper.addAttribute(profile, FacebookProfile.LOCATION, new FacebookObject(subJson));
        }
        // favorite_athletes
        subJson = json.get(FacebookProfile.FAVORITE_ATHLETES);
        if (subJson != null) {
            UserProfileHelper.addAttribute(profile, FacebookProfile.FAVORITE_ATHLETES,
                                           FacebookProfileHelper.getListFacebookObject(subJson));
        }
        // favorite_teams
        subJson = json.get(FacebookProfile.FAVORITE_TEAMS);
        if (subJson != null) {
            UserProfileHelper.addAttribute(profile, FacebookProfile.FAVORITE_TEAMS,
                                           FacebookProfileHelper.getListFacebookObject(subJson));
        }
        // significant_other
        subJson = json.get(FacebookProfile.SIGNIFICANT_OTHER);
        if (subJson != null) {
            UserProfileHelper.addAttribute(profile, FacebookProfile.SIGNIFICANT_OTHER, new FacebookObject(subJson));
        }
        // video_upload_limits
        // work
        subJson = json.get(FacebookProfile.WORK);
        if (subJson != null) {
            List<FacebookWork> work = new ArrayList<FacebookWork>();
            Iterator<JsonNode> workIterator = subJson.getElements();
            while (workIterator.hasNext()) {
                JsonNode jsonWork = workIterator.next();
                FacebookWork oneWork = new FacebookWork(jsonWork);
                work.add(oneWork);
            }
            UserProfileHelper.addAttribute(profile, FacebookProfile.WORK, work);
        }
        return profile;
    }
}
