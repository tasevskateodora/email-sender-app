import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../../shared/models';

export interface UserDto {
  id?: string;
  username: string;
  password?: string;
  enabled?: boolean;
  roleNames?: string[];
  createdAt?: string;
  updatedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = 'http://localhost:8080/api/v1/users';

  constructor(private http: HttpClient) {}

  getAll(): Observable<UserDto[]> {
    return this.http.get<UserDto[]>(this.apiUrl);
  }

  getById(id: string): Observable<UserDto> {
    return this.http.get<UserDto>(`${this.apiUrl}/${id}`);
  }

  create(user: UserDto): Observable<UserDto> {
    return this.http.post<UserDto>(this.apiUrl, user);
  }

  update(id: string, user: UserDto): Observable<UserDto> {
    return this.http.put<UserDto>(`${this.apiUrl}/${id}`, user);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  convertToUser(userDto: UserDto): User {
    return {
      id: userDto.id,
      username: userDto.username,
      password: userDto.password,
      enabled: userDto.enabled,
      roles: userDto.roleNames?.map(roleName => ({ name: roleName })) || [],
      createdAt: userDto.createdAt,
      updatedAt: userDto.updatedAt
    };
  }

  convertToUserDto(user: User): UserDto {
    return {
      id: user.id,
      username: user.username,
      password: user.password,
      enabled: user.enabled,
      roleNames: user.roles?.map(role => role.name) || [],
      createdAt: user.createdAt,
      updatedAt: user.updatedAt
    };
  }
}
