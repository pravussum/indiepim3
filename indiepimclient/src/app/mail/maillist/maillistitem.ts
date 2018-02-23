export class MailListItem {
  dateReceived: Date;
  sender: string;
  subject?: string;
  msgId: number;
  read: boolean;
  deleted: boolean;
  draft: boolean;
  tags?: MaillistItemTag[];
  hasAttachment: boolean;
  contentPreview?: string;
}

export class MaillistItemTag {
  tag: string;
  color: string;
}

export class MailListResult {
  messages: MailListItem[];
  totalCount: number;
}
