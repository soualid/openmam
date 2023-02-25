
export interface MetadataDefinition {
    id?: number;
    name: string;
    label?: string;
    orderNumber?: number;
    type?: MetadataType;
    allowedValues?: string[];
    referencedMetadataGroup?: MetadataGroup;
}
export interface MetadataGroup {
    id: number;
    name?: string;
    metadatas?: MetadataDefinition[];
    attachmentType?:MetadataAttachmentType;
}
export interface MetadataReference {
    id: number;
    representation?: string;
    dynamicMetadatas?: any;
}
export enum MetadataType {
    TEXT = 'TEXT',
    LONG_TEXT = 'LONG_TEXT',
    DATE = 'DATE',
    MULTI_VALUED = 'MULTI_VALUED',
    REFERENCE = 'REFERENCE',
}
export enum MetadataAttachmentType {
    MEDIA = 'MEDIA',
    VERSION = 'VERSION',
    REFERENCEABLE = 'REFERENCEABLE',
}  