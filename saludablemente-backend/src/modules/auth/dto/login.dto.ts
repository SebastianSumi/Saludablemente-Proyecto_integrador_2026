import { IsNotEmpty, IsString } from 'class-validator';

export class LoginRequestDto {
  @IsString()
  @IsNotEmpty({ message: 'El identificador no puede estar vacío' })
  identificador!: string;

  @IsString()
  @IsNotEmpty({ message: 'La contraseña no puede estar vacía' })
  password!: string;
}

export interface LoginResponseDto {
  token: string;
  email: string;
  nombre: string;
  roles: string[];
  expiraEn: number;
}
