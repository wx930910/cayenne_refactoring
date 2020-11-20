/*****************************************************************
 *   Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 ****************************************************************/
package org.apache.cayenne.modeler.editor;

import org.apache.cayenne.modeler.ProjectController;
import org.apache.cayenne.modeler.action.GenerateCodeAction;
import org.apache.cayenne.modeler.action.dbimport.ReverseEngineeringToolMenuAction;
import org.apache.cayenne.modeler.editor.cgen.domain.CgenTabController;
import org.apache.cayenne.modeler.editor.dbimport.domain.DbImportTabController;
import org.apache.cayenne.modeler.event.DomainDisplayEvent;
import org.apache.cayenne.modeler.event.DomainDisplayListener;
import org.apache.cayenne.modeler.event.EntityDisplayEvent;
import org.apache.cayenne.modeler.graph.DataDomainGraphTab;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * DataDomain editing tabs container 
 */
public class DataDomainTabbedView extends JTabbedPane 
    implements ChangeListener, DomainDisplayListener {
    
    ProjectController mediator;
    
    DataDomainGraphTab graphTab;
    private JScrollPane cgenView;
    private CgenTabController cgenTabController;
    private JScrollPane dbImportView;
    private DbImportTabController dbImportTabController;

    /**
     * constructor
     * @param mediator mediator instance
     */
    public DataDomainTabbedView(ProjectController mediator) {
        this.mediator = mediator;

        initView();
    }

    /**
     * create tabs
     */
    private void initView() {
      
        setTabPlacement(JTabbedPane.TOP);

        // add panels to tabs
        // note that those panels that have no internal scrollable tables
        // must be wrapped in a scroll pane
        JScrollPane domainView = new JScrollPane(new DataDomainView(mediator));
        addTab("Main", domainView);

        addChangeListener(this);
        mediator.addDomainDisplayListener(this);

        dbImportTabController = new DbImportTabController(mediator);
        dbImportView = new JScrollPane(dbImportTabController.getView());
        addTab("Db Import", dbImportView);

        cgenTabController = new CgenTabController(mediator);
        cgenView = new JScrollPane(cgenTabController.getView());
        addTab("Class Generation", cgenView);

        graphTab = new DataDomainGraphTab(mediator);
        addTab("Graph", graphTab);
    }

    public void stateChanged(ChangeEvent e) {
        if (getSelectedComponent() == graphTab) {
            graphTab.refresh();
        } else if(getSelectedComponent() == cgenView) {
            cgenTabController.getView().initView();
        } else if(getSelectedComponent() == dbImportView) {
            dbImportTabController.getView().initView();
        }
    }

    public void currentDomainChanged(DomainDisplayEvent e) {
        if (e instanceof EntityDisplayEvent) {
            //need select an entity
            setSelectedComponent(graphTab);
        }
        if(getSelectedComponent() == cgenView) {
            fireStateChanged();
        }
        if(e.getSource() instanceof GenerateCodeAction) {
            setSelectedComponent(cgenView);
        }
        if(getSelectedComponent() == dbImportView) {
            fireStateChanged();
        }
        if(e.getSource() instanceof ReverseEngineeringToolMenuAction) {
            setSelectedComponent(dbImportView);
        }
    }
}
