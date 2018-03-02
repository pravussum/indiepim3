import { TestBed, inject } from '@angular/core/testing';

import { MailviewService } from './mailview.service';

describe('MailviewService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MailviewService]
    });
  });

  it('should be created', inject([MailviewService], (service: MailviewService) => {
    expect(service).toBeTruthy();
  }));
});
