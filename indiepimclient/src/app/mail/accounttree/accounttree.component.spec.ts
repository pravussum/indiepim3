import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AccounttreeComponent } from './accounttree.component';

describe('AccounttreeComponent', () => {
  let component: AccounttreeComponent;
  let fixture: ComponentFixture<AccounttreeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AccounttreeComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AccounttreeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
