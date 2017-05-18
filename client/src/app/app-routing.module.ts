import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { DatasetComponent } from "./views/catalog/dataset/dataset.component";
import { DatasetListComponent } from "./views/catalog/dataset/dataset-list.component";

import { DataSetListComponent as EditorDataSetListComponent } from './views/editor/dataset/data-set-list.component';
import { DatasetViewComponent as EditorDataSetComponent } from './views/editor/dataset/dataset-view.component';
import { DataSetEditComponent } from "./views/editor/dataset/data-set-edit.component";
import { InstanceVariableEditComponent } from "./views/editor/dataset/instance-variable-edit.component";

const routes: Routes = [
    {
        path: 'catalog/datasets/:id',
        component: DatasetComponent
    },
    {
        path: 'catalog/datasets',
        component: DatasetListComponent
    },
    {
        path: 'catalog',
        redirectTo: 'catalog/datasets',
    },
    {
        path: 'editor/datasets/:datasetId/instanceVariables/new',
        component: InstanceVariableEditComponent,
    },
    {
        path: 'editor/datasets/:datasetId/instanceVariables/:instanceVariableId/edit',
        component: InstanceVariableEditComponent,
    },
    {
        path: 'editor/datasets/new',
        component: DataSetEditComponent,
    },
    {
        path: 'editor/datasets/:id/edit',
        component: DataSetEditComponent,
    },
    {
        path: 'editor/datasets/:id',
        component: EditorDataSetComponent,

    },
    {
        path: 'editor/datasets',
        component: EditorDataSetListComponent
    },
    {
        path: 'editor',
        redirectTo: 'editor/datasets',
    },
    {
        path: '',
        redirectTo: 'catalog/datasets',
        pathMatch: 'full'
    }
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule {
}
