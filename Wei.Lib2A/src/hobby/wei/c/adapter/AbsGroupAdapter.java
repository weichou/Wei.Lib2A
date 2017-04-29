/*
 * Copyright (C) 2016-present, Wei Chou (weichou2010@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hobby.wei.c.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseExpandableListAdapter;

import java.util.Collections;
import java.util.List;

/**
 * 配合{@link android.widget.ExpandableListView}的Adapter.
 *
 * @author Wei.Chou
 * @version 1.0, 10/05/2016
 */
public abstract class AbsGroupAdapter<Group, Child> extends BaseExpandableListAdapter {
    public final List<Group> EMPTY_G = Collections.emptyList();
    public final List<List<Child>> EMPTY_C = Collections.emptyList();
    private List<Group> mGroups = EMPTY_G;
    private List<List<Child>> mChildren = EMPTY_C;
    private LayoutInflater mInflater;

    public AbsGroupAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public void setDataSource(List<Group> groups, List<List<Child>> children) {
        mGroups = groups == null ? EMPTY_G : groups;
        mChildren = children == null ? EMPTY_C : children;
        if (mGroups.size() != mChildren.size()) {
            throw new IllegalArgumentException("groups.size()必须与children.size()相等。");
        }
        notifyDataSetChanged();
    }

    protected LayoutInflater getInflater() {
        return mInflater;
    }

    public final List<Group> getGroup() {
        return mGroups;
    }

    @Override
    public final Group getGroup(int groupPosition) {
        return mGroups.get(groupPosition);
    }

    @Override
    public final int getGroupCount() {
        return mGroups.size();
    }

    public final List<List<Child>> getChild() {
        return mChildren;
    }

    @Override
    public final Child getChild(int groupPosition, int childPosition) {
        return mChildren.get(groupPosition).get(childPosition);
    }

    @Override
    public final int getChildrenCount(int groupPosition) {
        return mChildren.get(groupPosition).size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
