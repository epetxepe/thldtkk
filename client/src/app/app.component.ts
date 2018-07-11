import { Component, OnInit } from '@angular/core'
import { Router, NavigationEnd, ActivatedRoute } from '@angular/router'
import { Title } from '@angular/platform-browser'
import { TranslateService } from '@ngx-translate/core'

import { PageIdentifier } from './utils/page-identifier'

import { Observable } from 'rxjs'
import 'rxjs/add/operator/filter';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/mergeMap';

import { BreadcrumbService } from './services-common/breadcrumb.service'
import { CurrentUserService } from './services-editor/user.service'
import { GrowlMessageService } from './services-common/growl-message.service'
import { User } from './model2/user'

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {

  public PageIdentifier = PageIdentifier
  currentPageType: PageIdentifier
  mainContainerClasses: any = {}
  hideNavBar: boolean = false

  currentUser: User

  constructor(
    public growlMessageService: GrowlMessageService,
    public translateService: TranslateService,
    private titleService: Title,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private currentUserService: CurrentUserService,
    private breadcrumbService: BreadcrumbService
  ) {
    translateService.setDefaultLang('en');
    translateService.use('en');
  }

  ngOnInit() {
    this.router.events
      .filter((event) => event instanceof NavigationEnd)
      .map(() => this.activatedRoute)
      .map((route) => {
        while (route.firstChild) route = route.firstChild;
        return route;
      })
      .filter((route) => route.outlet === 'primary')
      .mergeMap((route) => route.data)
      .subscribe(event => {
        let titleTranslationKey: string = event['title']
        let pageType: PageIdentifier = event['pageType']

        this.setPageTitle(titleTranslationKey, pageType)

        this.currentPageType = pageType | this.currentPageType
        this.hideNavBar = event['hideNavBar'] ? event['hideNavBar'] : false

        this.mainContainerClasses['main-container'] = true
        this.mainContainerClasses['has-sidebar'] = event['hasSidebar']

        this.mainContainerClasses['catalog'] = this.currentPageType == PageIdentifier.CATALOG
        this.mainContainerClasses['container'] = this.currentPageType == PageIdentifier.CATALOG

        this.mainContainerClasses['editor'] = this.currentPageType == PageIdentifier.EDITOR
        this.mainContainerClasses['container-fluid'] = this.currentPageType == PageIdentifier.EDITOR && !event['hasSidebar']

        this.mainContainerClasses['hiddenNavBar'] = this.hideNavBar
      })

    this.router.events.subscribe(event => {
      if (!(event instanceof NavigationEnd)) {
        return
      }
      // Scroll to top of the page after route changes
      window.scrollTo(0, 0)
      // Reset breadcrumb, views can/should update breadcrumb on init
      this.breadcrumbService.clearBreadcrumbs()
    })

    this.currentUserService.getCurrentUserObservable()
      .subscribe(user => this.currentUser = user)
  }

  private setPageTitle(translationKey: string, pageType: PageIdentifier):void {

    let validPageType: PageIdentifier = pageType != null ? pageType : this.currentPageType

    if(translationKey) {
      this.getPageTitle(translationKey).subscribe(title => {
        this.getPageTitleSuffix(validPageType).subscribe(suffix => {
          let fullTitle:string = suffix ? title + " - " + suffix: title
          this.titleService.setTitle(fullTitle)
        })
      })
    } // for routes without a set title, use only the generic suffix for titles
    else if(validPageType != null) {
      this.getPageTitleSuffix(validPageType).subscribe(suffix => {
        this.titleService.setTitle(suffix)
      })
    }
  }

  private getPageTitle(translationKey: string):Observable<string> {
    return this.translateService.get(translationKey)
  }

  private getPageTitleSuffix(pageType:PageIdentifier):Observable<string> {

    let suffix:Observable<string> = Observable.of("")

    if(pageType === PageIdentifier.CATALOG) {
      suffix = this.translateService.get('pageTitles.catalog.suffix');
    }

    else if(pageType === PageIdentifier.EDITOR) {
      suffix = this.translateService.get('pageTitles.editor.suffix');
    }
      return suffix
  }

  handleLogout(): void {
    this.currentUserService.logout()
  }

}
