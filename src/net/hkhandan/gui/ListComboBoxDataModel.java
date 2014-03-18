/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.hkhandan.gui;

import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

public class ListComboBoxDataModel extends AbstractListModel implements ComboBoxModel {
    private int selectedIndex = 0;
    private List items = null;
    
    public ListComboBoxDataModel(List<?> objects) {
        items = objects;
        fireIntervalAdded(this, 0, objects.size() - 1);
    }
    
    public ListComboBoxDataModel(Object[] objects) {
        items = Arrays.asList(objects);
        fireIntervalAdded(this, 0, objects.length - 1);
    }
    
    public void setItems(List<?> objects) {
        fireIntervalRemoved(this, 0, items.size() - 1);
        items = objects;
        fireIntervalAdded(this, 0, objects.size() - 1);
    }
    
    public void setItems(Object[] objects) {
        fireIntervalRemoved(this, 0, items.size());
        items = Arrays.asList(objects);
        fireIntervalAdded(this, 0, objects.length - 1);
    }

    public void setSelectedIndex(int index) {
        selectedIndex = index;
        fireContentsChanged(this, -1, -1);
    }
    
    public int getSelectedIndex() {
        return selectedIndex;
    }

    @Override
    public void setSelectedItem(Object anItem) {
        if(items == null)
            return;
        for(int i = items.size() - 1; i >= 0; i--) {
            if(items.get(i) == anItem)
                setSelectedIndex(i);
        }
    }

    @Override
    public Object getSelectedItem() {
        return items.get(selectedIndex);
    }

    @Override
    public int getSize() {
        return items.size();
    }

    @Override
    public Object getElementAt(int index) {
        return items.get(index);
    }
}