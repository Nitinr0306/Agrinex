/** The envelope of every file in __mocks__/api (FRONTEND_CONSUMPTION.md §6; __mocks__/api/README.md). */
export interface MockEnvelope {
  synthetic: true;
  notice: string;
  operationId: string;
  scenario: string;
  request: {
    method: string;
    path: string;
    headers?: Record<string, string>;
    body?: unknown;
  };
  response: {
    status: number;
    headers: Record<string, string>;
    body: unknown;
  };
}
