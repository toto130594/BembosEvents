FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build

WORKDIR /src

COPY BembosEvents.Web.csproj ./

RUN dotnet restore BembosEvents.Web.csproj

COPY . .

RUN dotnet publish BembosEvents.Web.csproj \
    -c Release \
    -o /app/publish \
    /p:UseAppHost=false \
    --no-restore

FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS runtime

WORKDIR /app

ENV ASPNETCORE_URLS=http://+:8080
ENV ASPNETCORE_ENVIRONMENT=Production

EXPOSE 8080

COPY --from=build /app/publish .

ENTRYPOINT ["dotnet", "BembosEvents.Web.dll"]
