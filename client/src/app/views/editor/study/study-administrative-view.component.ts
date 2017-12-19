import { ActivatedRoute } from '@angular/router';
import { Component, OnInit } from '@angular/core';
import { TranslateService } from "@ngx-translate/core";
import { Title } from '@angular/platform-browser'
import { LangPipe } from '../../../utils/lang.pipe'

import { BreadcrumbService } from '../../../services-common/breadcrumb.service'
import { Study } from "../../../model2/study";
import { EditorStudyService } from '../../../services-editor/editor-study.service'
import { StudySidebarActiveSection } from './sidebar/study-sidebar-active-section'
import { ConfidentialityClass } from '../../../model2/confidentiality-class'

@Component({
  templateUrl: './study-administrative-view.component.html'
})
export class StudyAdministrativeViewComponent implements OnInit {

  study: Study
  language: string

  sidebarActiveSection = StudySidebarActiveSection.ADMINISTRATIVE_INFORMATION

  confidentialityClassType = ConfidentialityClass

  constructor(private breadcrumbService: BreadcrumbService,
              private studyService: EditorStudyService,
              private route: ActivatedRoute,
              private translateService: TranslateService,
              private titleService: Title,
              private langPipe: LangPipe) {
    this.language = this.translateService.currentLang
  }

  ngOnInit() {
    this.getStudy();
  }

  getStudy() {
    const studyId = this.route.snapshot.params['studyId']
    this.studyService.getStudy(studyId)
      .subscribe(study => {
        this.study = study
        this.breadcrumbService.updateEditorBreadcrumbsForStudyDatasetAndInstanceVariable(this.study)
        this.updatePageTitle()
      })
  }

  updatePageTitle():void {
    if(this.study.prefLabel) {
      let translatedLabel:string = this.langPipe.transform(this.study.prefLabel)
      let bareTitle:string = this.titleService.getTitle();
      this.titleService.setTitle(translatedLabel + " - " + bareTitle)
    }
  }


}