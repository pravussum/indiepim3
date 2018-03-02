import { TestBed, inject } from '@angular/core/testing';

import { TaglineageService } from './taglineage.service';

describe('TaglineageService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TaglineageService]
    });
  });

  it('should be created', inject([TaglineageService], (service: TaglineageService) => {
    expect(service).toBeTruthy();
  }));
});
