export class MailItem {
  id : number;
  subject? : string;

  read : boolean;
  deleted : boolean;
  draft : boolean;
  hasAttachment : boolean;
  star : number;

  tags : MailItemTag[];
  sender : string;
  senderEmail : string;
  receiver : string[];
  cc : string[];
  bcc : string[];
  dateReceived: Date;

  contentHtml : string;
  contentText : string;
  accountId : number;

  attachments : MailItemAttachment[];

}

export class MailItemTag {
  tag: string;
  color: string;
}

export class MailItemAttachment {
  id : number;
  filename : string;
  mimeType : string;
}
