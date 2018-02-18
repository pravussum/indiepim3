export class MaillistView {

  constructor(private _searchTerm? : string,
              private _accountId? : number,
              private _tagName? : string,
              private _tagLineageId? : number,
              private _read?: boolean) { }

  isViewAll() {
    return !(this._searchTerm || this._accountId || this._tagName || this._tagLineageId || this._read);
  }

  getDisplayText() {
    if(this._searchTerm) {
      return "Search results for '" + this._searchTerm + "'";
    }
    if(this._accountId) {
      return "Showing all account messages."
    }
    if(this._tagName) {
      return "Showing messages for tag " + this._tagName;
    }
    if(this._tagLineageId) {
      return "Showing messages for tag hierarchy node.";
    }
    if(typeof this._read  != "undefined") {
      if(this._read == false || !this._read)
        return "Showing unread messages.";
      else
        return "Showing read messages.";
    }
    return "Showing all messages";
  }


  get searchTerm(): string {
    return this._searchTerm;
  }

  get accountId(): number {
    return this._accountId;
  }

  get tagName(): string {
    return this._tagName;
  }

  get tagLineageId(): number {
    return this._tagLineageId;
  }

  get read(): boolean {
    return this._read;
  }
}
