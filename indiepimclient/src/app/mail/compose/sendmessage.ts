export class SendMessage {
  accountId: number;
  subject?: String;
  to: String[];
  cc?: String;
  bcc?: String;
  content?: String;
  isHtml: boolean;
}
