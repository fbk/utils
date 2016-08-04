/*
 * Copyright (2013) Fondazione Bruno Kessler (http://www.fbk.eu/)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.fbk.utils.twm;

/**
 * Created with IntelliJ IDEA. User: giuliano Date: 1/22/13 Time: 6:23 PM To change this template
 * use File | Settings | File Templates.
 */
public abstract class Index {

    public final static int DEFAULT_NOTIFICATION_POINT = 100000;

    protected int notificationPoint;

    protected String indexName;

    protected Index(final String indexName) {
        this(indexName, DEFAULT_NOTIFICATION_POINT);
    }

    protected Index(final String indexName, final int notificationPoint) {
        this.indexName = indexName;
        this.notificationPoint = DEFAULT_NOTIFICATION_POINT;
    }

    public String getIndexName() {
        return this.indexName;
    }

    public int getNotificationPoint() {
        return this.notificationPoint;
    }

    public void setNotificationPoint(final int notificationPoint) {
        this.notificationPoint = notificationPoint;
    }
}
