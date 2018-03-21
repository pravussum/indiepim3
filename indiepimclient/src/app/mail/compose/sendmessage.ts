export class SendMessage {
  accountId: number;
  subject?: String;
  to: Array<String> = [];
  cc: Array<String> = [];
  bcc: Array<String> = [];
  content?: String;
  isHtml: true;
}
