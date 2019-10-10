import { Component, EventEmitter, Input, Output, OnChanges, SimpleChanges, ViewChild, ViewChildren, QueryList, ViewEncapsulation } from '@angular/core';
import { Entity } from '../../../../../models/index';
import { MdlDialogService } from '@angular-mdl/core';

import * as _ from 'lodash';
import * as moment from 'moment';
import { Mapping } from "../../../../mappings/mapping.model";
import { EnvironmentService } from '../../../../../services/environment';
import { EntityTableUiComponent } from './entity-table-ui.component';

import {MatDialog, MatPaginator, MatSort, MatTable, MatTableDataSource} from "@angular/material";
import { Step } from '../../../models/step.model';
import {animate, state, style, transition, trigger} from '@angular/animations';

//import { ManageFlowsService } from "../../../services/manage-flows.service";

@Component({
  selector: 'app-mapping-ui',
  templateUrl: './mapping-ui.component.html',
  styleUrls: ['./mapping-ui.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class MappingUiComponent implements OnChanges {

  @Input() mapping: Mapping = new Mapping();
  @Input() targetEntity: Entity = null;
  @Input() conns: object = {};
  @Input() sampleDocSrcProps: Array<any>;
  @Input() sampleDocNestedProps: Array<any>;
  @Input() docUris: Array<any>;
  @Input() editURIVal: string = '';
  @Input() step: Step;
  @Input() functionLst: object;
  @Input() entityName: string;
  @Input() entityNested: Entity;

  @Input() entityProps: any;
  @Input() nmspace: object;
  @Output() updateURI = new EventEmitter();
  @Output() updateMap = new EventEmitter();

  private uriOrig: string = '';
  private connsOrig: object = {};

  public valMaxLen: number = 15;
  public isVersionCompatibleWithES: boolean = false;

  public filterFocus: object = {};
  public filterText: object = {};

  public editingURI: boolean = false;
  public editingSourceContext: boolean = false;
  
  displayedColumns = ['key', 'val'];

  // Entity table column menu
  entityTblCols: any = [
    { id: 'name', label: 'Name', shown: true },
    { id: 'datatype', label: 'Type', shown: true },
    { id: 'expression', label: 'Expression', shown: true },
    { id: 'value', label: 'Value', shown: true }
  ];
  colsShown: Array<string> = this.entityTblCols.map(c => {
    if (c.shown) return c.id;
  });

  dataSource: MatTableDataSource<any>;
  mapExpresions = {};
  mapExpValue: Array<any> = [];
  runningStatus = false;
  nestedEntityStatus: boolean = false;
  entName: string = '';
  isExpansionDetailRow : boolean = false;
  expandedElement: any;
  public fncLst: Object;
  dataSourceEntity: Array<any> = [];

    disableURINavLeft: boolean = false;
    disableURINavRight: boolean = false;
    uriIndex = 0;

  @ViewChild(MatTable)
  table: MatTable<any>;

  @ViewChild(MatPaginator)
  paginator: MatPaginator;

  @ViewChild(MatSort)
  sort: MatSort;

  @ViewChildren('fieldName') fieldName:QueryList<any>;

  ngOnInit(){
    if (!this.dataSource){
      this.dataSource = new MatTableDataSource<any>(this.sampleDocNestedProps);
    }
    if(_.isEmpty(this.mapExpresions)) {
      this.mapExpresions = this.conns;
    }
    this.isVersionCompatibleWithES = this.envService.settings.isVersionCompatibleWithES;
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }
  updateDataSource() {
    if (!this.dataSource){
      this.dataSource = new MatTableDataSource<any>(this.sampleDocNestedProps);
       }
    this.dataSource.data = this.sampleDocNestedProps;
  }

  renderRows(): void {
    this.updateDataSource();
    if(_.isEmpty(this.mapExpresions)) {
      this.mapExpresions = this.conns;
    }
  }

  onNavigateURIList(index) {
    if (index < 0 || index > this.docUris.length) {
      this.uriIndex = index;
      if (index < 0) {
        this.disableURINavLeft = true;
      } else {
        this.disableURINavRight = true;
      }


    } else {
      this.disableURINavLeft = false;
      this.disableURINavRight = false;

      this.uriIndex = index;
      this.editURIVal = this.docUris[index];

      this.onUpdateURINewUI();
    }

  }
  onUpdateURINewUI(){
    if (Object.keys(this.conns).length > 0) {
      this.editingURI = false;
      this.updateURI.emit({
        uri: this.editURIVal,
        uriOrig: this.mapping.sourceURI,
        conns: this.conns,
        connsOrig: this.connsOrig,
        save: true
      });
    } else {
      this.editingURI = false;
      this.updateURI.emit({
        uri: this.editURIVal,
        uriOrig: this.mapping.sourceURI,
        conns: this.conns,
        connsOrig: {},
        save: true
      });
    }
  }

  onUpdateURI() {
    if (Object.keys(this.conns).length > 0) {
      let result = this.dialogService.confirm(
          'Changing your source document will remove<br/>existing property selections. Proceed?',
          'Cancel', 'OK');
      result.subscribe( () => {
          this.conns = {};
          this.editingURI = false;
          this.updateURI.emit({
            uri: this.editURIVal,
            uriOrig: this.mapping.sourceURI,
            conns: this.conns,
            connsOrig: this.connsOrig,
            save: true
          });
        },(err: any) => {
          console.log('source change aborted');
          this.editingURI = false;
        },
        () => {}
      );
    } else {
      this.editingURI = false;
      this.updateURI.emit({
        uri: this.editURIVal,
        uriOrig: this.mapping.sourceURI,
        conns: this.conns,
        connsOrig: {},
        save: true
      });
    }
  }

  /**
   * Cancel the editing of the source document URI.
   */
  cancelEditURI() {
    this.editURIVal = this.mapping.sourceURI;
    this.editingURI = false;
  }

  /**
   * Handle "Enter" keypress for the source document URI box.
   * @param event Event object
   */
  keyPressURI(event) {
    if (event.key === 'Enter') {
      this.onUpdateURI();
    }
  }

  /**
   * Handle when edit URI is not found.
   * @param uri URI not found
   */
  uriNotFound(uri) {
    let result = this.dialogService.alert(
      'No document found. You must ingest source documents',
      'OK'
    );
    result.subscribe( () => {
        this.editURIVal = this.mapping.sourceURI;
        // rollback to conns from previous URI
        if (!_.isEmpty(this.connsOrig)) {
          this.conns = this.connsOrig;
        }
      },
      () => {},
      () => {}
    )
  }

  constructor(
    private dialogService: MdlDialogService,
    private envService: EnvironmentService,
    public dialog: MatDialog
  ) {}

  /**
   * Handle changes of component properties.
   * @param changes SimpleChanges object with change information.
   */
  ngOnChanges(changes: SimpleChanges) {
    // Keep values up to date when mapping changes
    if (changes.mapping) {
      this.editURIVal = this.mapping.sourceURI;
    }
    if (changes.conns) {
      this.connsOrig = _.cloneDeep(changes.conns.currentValue);
    }
    if (changes.sampleDocSrcProps){
      this.renderRows();
    } 
    if (changes.entityNested && changes.entityNested.currentValue){
      // Get props from target entity for passing to child
      this.entityProps = 
        this.entityNested.definitions[this.targetEntity.info.title].properties;
    } 
  }

  handleSelection(name, expr, nested): void {
    console.log('mapping-ui handleSelection', name, expr, nested);
    if (nested === true) {
      // New nested version 
      this.conns = expr;
    } else {
      // Legacy flat version
      this.conns[name] = expr;
    }
    if (!_.isEqual(this.conns, this.connsOrig)) {
      this.onSaveMap(nested);
    }
  }

  onHandleSelection(event): void {
    console.log('mapping-ui onHandleSelection', event);
    this.handleSelection(event.name, event.expr, true);
  }

  /**
   * Clear a property selection from source menu
   * @param event Event object, used to stop propagation
   * @param entityPropName Entity property name mapping to clear
   */
  clearSelection(event, entityPropName): void {
    if (this.conns[entityPropName])
      delete this.conns[entityPropName];
    if (!_.isEqual(this.conns, this.connsOrig)) {
      this.onSaveMap(false);
    }
    this.editingURI = false; // close edit box if open
    event.stopPropagation();
  }

  /**
   * Get property objects of source document
   * @param entityPropName Entity property name mapping to lookup
   * @param srcKey 'key', 'val' or 'type'
   * @returns {String} Value of the src data requested
   */
  getConnSrcData(entityPropName, srcKey): string {
    let data;
    let propertyKey = this.conns[entityPropName];

    if (this.sampleDocSrcProps.length > 0 && this.conns[entityPropName]) {
      let obj = _.find(this.sampleDocSrcProps, function(o) { return o && (o.key === propertyKey); });
      if (obj) {
        data = obj[srcKey];
      }
    }

    return (data) ? String(data) : data;
  }

  /**
   * Handle save event by emitting connection object.
   */
  onSaveMap(nested) {
    this.updateMap.emit({
      conns: this.conns,
      nested: nested
    });
    this.connsOrig = _.cloneDeep(this.conns);
  }

  /**
   * Have there been new selections since last map save?
   * @returns {boolean}
   */
  mapChanged() {
    return !_.isEqual(this.conns, this.connsOrig);
  }

  /**
   * Interpret the datatype of a property value
   * Recognize all JSON types: array, object, number, boolean, null
   * Also do a basic interpretation of dates (ISO 8601, RFC 2822)
   * @param value Property value
   * @returns {string} datatype ("array"|"object"|"number"|"date"|"boolean"|"null")
   */
  getType(value: any): string {
    let result = '';
    let RFC_2822 = 'ddd, DD MMM YYYY HH:mm:ss ZZ';
    if (_.isArray(value)) {
      result = 'array';
    } else if (_.isObject(value)) {
      result = 'object';
    }
    // Quoted numbers (example: "123") are not recognized as numbers
    else if (_.isNumber(value)) {
      result = 'number';
    }
    // Do not recognize ordinal dates (example: "1981095")
    else if (moment(value, [moment.ISO_8601, RFC_2822], true).isValid() && !/^\d+$/.test(value)) {
      result = 'date';
    } else if (_.isBoolean(value)) {
      result = 'boolean';
    } else if (_.isNull(value)) {
      result = 'null';
    } else {
      result = 'string';
    }
    return result;
  }

  /**
   * Should datatype be displayed with quotes?
   * @param property datatype
   * @returns {boolean}
   */
  isQuoted(type) {
    let typesToQuote = ['string', 'date'];
    return _.indexOf(typesToQuote, type) > -1;
  }

  /**
   * Trim start of long string and add prefix ('...trimmed-string'
   * @param str String to trim
   * @param num Character threshold
   * @param prefix Prefix to add
   * @returns {any} Trimmed string
   */
  getLastChars(str, num, prefix) {
    prefix = prefix ? prefix : '...';
    let result = str;
    if (typeof str === 'string' && str.length > num) {
      result = prefix + str.substr(str.length - num);
    }
    return result;
  }

  getInitialChars(str, num, suffix) {
    suffix = suffix ? suffix : '...';
    let result = str;
    if (typeof str === 'string' && str.length > num) {
      result = str.substr(0, num) + suffix;
    }
    return result;
  }

   /**
    * Return string if sufficiently long, otherwise empty string
    * @param str String to return
    * @param num Character threshold
    * @returns {any} string
    */
   getURITooltip(str, num) {
     let result = '';
     if (typeof str === 'string' && str.length > num) {
       result = str;
     }
     return result;
   }

  /**
   * Does entity property have an element range index set?
   * @param name Name of property
   * @returns {boolean}
   */
  hasElementRangeIndex(name) {
    return _.includes(this.targetEntity.definition.elementRangeIndex, name);
  }

  /**
   * Does entity property have a path range index set?
   * @param name Name of property
   * @returns {boolean}
   */
  hasRangeIndex(name) {
    return _.includes(this.targetEntity.definition.rangeIndex, name);
  }

  /**
   * Does entity property have a word lexicon set?
   * @param name Name of property
   * @returns {boolean}
   */
  hasWordLexicon(name) {
    return _.includes(this.targetEntity.definition.wordLexicon, name);
  }

  /**
   * Is an entity property required?
   * @param name Name of property
   * @returns {boolean}
   */
  isRequired(name) {
    return _.includes(this.targetEntity.definition.required, name);
  }

  /**
   * Is an entity property personally identifiable information?
   * @param name Name of property
   * @returns {boolean}
   */
  isPII(name) {
    return _.includes(this.targetEntity.definition.pii, name);
  }

      /**
   * Is an entity property the primary key?
   * @param name Name of property
   * @returns {boolean}
   */
  isPrimaryKey(name) {
    return _.includes(this.targetEntity.definition.primaryKey, name);
  }

  OpenFullSourceQuery() {
    let result = this.dialogService.alert(
      this.step.options.sourceQuery,
      'OK'
    );
    result.subscribe();

  }

  //Indenting the nested levels
  IndentCondition(prop) {
    let count = prop.split('/').length - 1;
    let indentSize = 20*count;
  
    let style = {'text-indent': indentSize+'px'}
  return style
  }

  // Removing duplicate entries in the source dataset
  uniqueSourceFields(source) {
    let uniqueSrcFields = [];
    source.forEach(obj => {
      uniqueSrcFields.push(obj.key);
    });
    
    return uniqueSrcFields.filter((item, index) => uniqueSrcFields.indexOf(item) === index);
  }

  // Attach namespace, if the source is an xml document
  displaySourceField(field): string {
    let fieldValue = "";
    if(this.nmspace && field in this.nmspace) {
      fieldValue = this.nmspace[field] + ":"+ field.split('/').pop();
    }
    else {
      fieldValue = field.split('/').pop();
    }
    return fieldValue;
  } 

  // Handle selection in entity table column menu
  colToggle(id) {
    let i = this.entityTblCols.findIndex(col => {
      return col.id == id;
    })
    this.entityTblCols[i].shown = !this.entityTblCols[i].shown;
    let newColsShown = [];
    this.entityTblCols.map(c => {
      if (c.shown) {
        newColsShown.push(c.id);
      }
    });
    this.colsShown = newColsShown;
  }

}
