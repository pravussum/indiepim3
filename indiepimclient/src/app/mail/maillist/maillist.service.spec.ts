import { TestBed, inject } from '@angular/core/testing';

import { MaillistService } from './maillist.service';

describe('MaillistService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MaillistService]
    });
  });

  it('should be created', inject([MaillistService], (service: MaillistService) => {
    expect(service).toBeTruthy();
  }));
});
