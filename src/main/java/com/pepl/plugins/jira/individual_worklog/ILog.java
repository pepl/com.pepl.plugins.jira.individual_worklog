package com.pepl.plugins.jira.individual_worklog;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.security.Permissions;

public class ILog extends AbstractJiraContextProvider {
    @Override
    public Map getContextMap(ApplicationUser user, JiraHelper jiraHelper) {
        Map contextMap = new HashMap();
        Issue currentIssue = (Issue) jiraHelper.getContextParams().get("issue");
        List < Worklog > worklogs = ComponentAccessor.getWorklogManager().getByIssue(currentIssue);
        Map < String, String > myMap = new HashMap < String, String > ();
        boolean hasPermission = ComponentAccessor.getPermissionManager().hasPermission(Permissions.WORK_ISSUE, currentIssue, user);
        List < Long > value = new ArrayList < Long > ();
        List < String > key = new ArrayList < String > ();
        if (!worklogs.isEmpty()) {
            for (Worklog wl: worklogs) {
                value.add(wl.getTimeSpent());
                key.add(wl.getAuthor());
            }
        }
        HashMap < String, HashMap > realMap = new HashMap < String, HashMap > ();
        HashMap < String, Long > newhm = getValueMap(key, value);
        Iterator entries = newhm.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();
            String newkey = (String) entry.getKey();
            Long newvalue = (Long) entry.getValue();
            HashMap newTime = getTime(newvalue);
            ApplicationUser workloguser = ComponentAccessor.getUserManager().getUserByKey(newkey);
            String username = newkey;
            if ( !ComponentAccessor.getUserKeyService().getUsernameForKey(newkey).isEmpty() ) {
                username = ComponentAccessor.getUserManager().getUserByKey(newkey).getDisplayName();
            }
            realMap.put(username, newTime);
        }
        contextMap.put("realMap", realMap);
        contextMap.put("hasPermission", hasPermission);
        return contextMap;
    }
    public HashMap < String, Long > getValueMap(List < String > key, List < Long > value) {
        Map newmap = new HashMap();
        long count = 0;
        for (int i = 0; i < value.size(); i++) {
            for (int j = 0; j < value.size(); j++) {
                if (key.get(i).equals(key.get(j))) {
                    count = count + value.get(j);
                }
            }
            newmap.put(key.get(i), count);
            count = 0;
        }
        return (HashMap < String, Long > ) newmap;
    }
    public HashMap < Integer, Integer > getTime(long seconds) {
        long hours = TimeUnit.SECONDS.toHours(seconds);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds) * 60);
        HashMap times = new HashMap < Integer, Integer > ();
        times.put(hours, minutes);
        return times;
    }
}
