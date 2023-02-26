export interface ScanResult {
    message: string;
}
export interface CreateMediaResult {
    id: number;
}
export interface CreateVersionResult {
    id: number;
}
export interface Media {
    id?: number;
    name?: string;
    video?: MediaStream;
    elements?: MediaElement[];
    dynamicMetadatas?: any;
    variantsLocation?: Location;
    versions?: MediaVersion[];
}
export interface MediaVersion {
    id?: number;
    name?: string;
    language?: string;
    audio?: MediaStream;
    subtitles?: MediaStream[];
    dynamicMetadatas?: any;
}
export interface MediaElement {
    filename?: string;
    fullMetadatas?: string;
    location?: Location;
    streams?: MediaStream[];
}
export interface MediaStream {
    status?: string;
    type?: string;
    streamIndex?: number;
    codecName?: string;
    codecType?: string;
    codecLongName?: string;
    codecTagString?: string;
}

export interface Location {
    name?: string;
    type?: string;
    path?: string;
    id?: number;
}

export interface Audio {
    name?: string;
    code?: string;
}
export interface Subtitle {
    name?: string;
    code?: string;
}