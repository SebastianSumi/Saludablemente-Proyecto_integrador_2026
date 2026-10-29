/**
 * Representación compatible con Spring Data Page<T> consumida por el frontend Angular.
 */
export interface SpringPage<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number; // 0-indexado
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface PaginationParams {
  page?: number;
  size?: number;
}

export function buildSpringPage<T>(
  content: T[],
  totalElements: number,
  page: number = 0,
  size: number = 20
): SpringPage<T> {
  const safeSize = size <= 0 ? 20 : size;
  const safePage = page < 0 ? 0 : page;
  const totalPages = Math.ceil(totalElements / safeSize);

  return {
    content,
    totalElements,
    totalPages,
    size: safeSize,
    number: safePage,
    first: safePage === 0,
    last: safePage >= totalPages - 1 || totalPages === 0,
    empty: content.length === 0,
  };
}
