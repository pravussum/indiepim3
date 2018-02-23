export class MaillistView {

  private _offset: number;
  private _totalSize: number;

  constructor(private _searchTerm? : string,
              private _accountId? : number,
              private _tagName? : string,
              private _tagLineageId? : number,
              private _read?: boolean) {

    this._offset = 0;
  }

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

  set offset(value: number) {
    this._offset = value;
  }

  get offset(): number {
    return this._offset;
  }

  get totalSize(): number {
    return this._totalSize;
  }

  set totalSize(value: number) {
    this._totalSize = value;
  }
}
