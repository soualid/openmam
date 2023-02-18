export interface ScanResult {
    message: string;
}
export interface CreateMediaResult {
    id: number;
}
export interface Media {
    id?: number;
    name?: string;
    elements?: MediaElement[];
}
export interface MediaElement {
    filename?: string;
    fullMetadatas?: string;
    location?: Location;
    streams?: MediaStream[];
}
export interface MediaStream {
    status?: string;
    streamIndex?: number;
    type?: string;
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