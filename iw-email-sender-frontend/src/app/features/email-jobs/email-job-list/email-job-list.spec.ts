import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmailJobListComponent} from './email-job-list';

describe('EmailJobList', () => {
  let component: EmailJobListComponent;
  let fixture: ComponentFixture<EmailJobListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmailJobListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EmailJobListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
