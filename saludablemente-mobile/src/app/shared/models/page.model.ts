/**
 * Respuesta paginada estándar de Spring Data.
 * Reutilizable por cualquier feature que consuma endpoints con Pageable.
 */
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
